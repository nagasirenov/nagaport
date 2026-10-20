#!/usr/bin/env python3
"""Fail-closed exact receiver/call-boundary edits; preserve argument interiors verbatim."""
import json,re,hashlib,argparse
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
def manifest():return json.loads((HERE/'manifest.json').read_text())
def masked(s):
 return re.sub(r'//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',lambda m:' '*len(m[0]),s)
def edits(text,record):
 mask=masked(text);ranges=[]
 for e in record['edits']:
  anchors=list(re.finditer(re.escape(e['left_context']+e['before_prefix']),text))
  matches=list(re.finditer(re.escape(e['before_invocation']),text))
  if len(anchors)==1:
   a=anchors[0].start()+len(e['left_context']);start=a+len(e['before_prefix'])
  elif len(matches)==e['invocation_total']:
   hit=matches[e['invocation_ordinal']];a=hit.start();start=a+len(e['before_prefix'])
  else:
   matches=list(re.finditer(re.escape(e['before_prefix']),text));assert len(matches)==e['prefix_total'],('Call prefix drift',e['before_prefix'],len(matches),e['prefix_total'])
   hit=matches[e['prefix_ordinal']];a=hit.start();start=hit.end()
  op=mask.rfind('(',a,start);assert op>=a
  depth=1;b=op+1
  while depth and b<len(mask):depth+=(mask[b]=='(')-(mask[b]==')');b+=1
  assert depth==0;close=b-1
  # Keep all argument bytes and comments, only replace the exact boundary tokens.
  suffix=e['before_suffix'];assert text[b-len(suffix):b]==suffix
  ranges.extend([(a,start,e['after_prefix']),(b-len(suffix),b,e['after_suffix'])])
 ranges.sort();assert all(ranges[i][1]<=ranges[i+1][0] for i in range(len(ranges)-1)), 'Overlapping boundary edits'
 return ranges
def transform(text,record):
 for a,b,v in reversed(edits(text,record)):text=text[:a]+v+text[b:]
 return text
def restore(text,record):
 # Stored postimage positions provide exact inverse on this isolated snapshot.
 ranges=[];delta=0
 original=[]
 for e in record['edits']:original.extend([(e['start'],e['a'],e['before_prefix'],e['after_prefix']),(e['b'],e['end'],e['before_suffix'],e['after_suffix'])])
 for a,b,old,new in sorted(original):
  x=a+delta;assert text[x:x+len(new)]==new; ranges.append((x,x+len(new),old));delta+=len(new)-(b-a)
 for a,b,v in reversed(ranges):text=text[:a]+v+text[b:]
 return text
def verify():
 n=0
 for rel,r in manifest()['files'].items():
  new=(ROOT/rel).read_bytes().decode();old=restore(new,r)
  assert hashlib.sha256(old.encode()).hexdigest()==r['before_sha256'];assert hashlib.sha256(new.encode()).hexdigest()==r['after_sha256'];assert transform(old,r)==new
  for prefix in set(e['before_prefix'] for e in r['edits']):
   try:transform(old.replace(prefix,prefix.replace('getBlock','BROKEN')),r)
   except AssertionError:pass
   else:raise AssertionError('Drift accepted')
  n+=len(r['edits'])
 print('PASS: %d reversible exact call boundaries preserve every unrelated byte and argument interior'%n)
if __name__=='__main__':verify()
