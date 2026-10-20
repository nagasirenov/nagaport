#!/usr/bin/env python3
"""Exact CP80 whole-source inverse for parent historical audit composition."""
import hashlib,json,pathlib
H=pathlib.Path(__file__).resolve().parent
M=json.loads((H/'source.json').read_text())
def reverse_source(relative,text):
 if str(relative).replace('\\','/')!=M['path']:return text
 assert hashlib.sha256(text.encode()).hexdigest()==M['after'],'DiFurnace full source drift'
 for old,new in reversed(M['edits']):
  assert new in text
  text=text.replace(new,old)
 assert hashlib.sha256(text.encode()).hexdigest()==M['before'],'DiFurnace CP80 reverse mismatch'
 return text
if __name__=='__main__':
 text=(H.parents[2]/M['path']).read_text();reverse_source(M['path'],text)
 for mutation in [text+'\n',text.replace('fuel -= 1;','fuel -= 2;'),text.replace('processingSpeed = 400','processingSpeed = 399')]:
  try:reverse_source(M['path'],mutation)
  except AssertionError:pass
  else:raise AssertionError('source mutation survived')
 print('PASS exact complete-source inverse and three rejected source mutations')
