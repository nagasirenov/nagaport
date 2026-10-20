#!/usr/bin/env python3
"""Newest exact EMPTY-boundary layer, BEFORE callbacks -> naming -> sided -> old audits.
All group manifests remain independent and fail closed; no Git baseline objects.
"""
import importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
_furnace_spec=importlib.util.spec_from_file_location('furnace_slice_audit',ROOT/'porting/tests/check-furnace-slice-source.py')
_furnace=importlib.util.module_from_spec(_furnace_spec);_furnace_spec.loader.exec_module(_furnace)
NAMES=['check-machine-core-source.py','check-inventory-ingress-source.py','check-machine-consumer-source.py','check-machine-drops-source.py','check-container-clicks.py','check-native-clears-source.py']
GROUPS=[];FILES={}
for index,name in enumerate(NAMES):
 spec=importlib.util.spec_from_file_location('empty_boundary_'+str(index),ROOT/'porting/tests'/name)
 module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module);GROUPS.append(module)
 for relative in module.FILES:
  assert relative not in FILES,('overlapping boundary source groups',relative)
  FILES[relative]=module

def reverse_source(relative,text):
 text=_furnace.reverse_source(relative,text)
 module=FILES.get(relative)
 return text if module is None else module.reverse_source(relative,text)

def main():
 _furnace.main()
 for relative in FILES:
  text=(ROOT/relative).read_bytes().decode('utf-8');reverse_source(relative,text)
  try:reverse_source(relative,text+'\n')
  except AssertionError:pass
  else:raise AssertionError((relative,'drift accepted'))
 print('PASS %d exact EMPTY-boundary source reversals and drift rejections across six disjoint groups'%len(FILES))
if __name__=='__main__':main()
