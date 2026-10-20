import pathlib,re,json,hashlib
H=pathlib.Path(__file__).resolve().parent;M=H.parents[2]
def body_at(s,a):
 i=a;depth=1
 while depth:depth+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[a:i-1].strip()
helpers={'CTContext':'stateContainer','ConveyorModelState':'create','FoundryModelState':'create','MultipassContext':'stateContainer','PlantBlockState':'create','SideRotationModelState':'create','SolarMirrorModelState':'create','SpotlightModelState':'create','UniversalBlockState':'create'}
creates=[];setters=[];helper_rows=[];corpus={}
for p in sorted((M/'src/main/java').rglob('*.java')):
 s=p.read_text();rel=p.relative_to(M).as_posix();corpus[rel]=hashlib.sha256(p.read_bytes()).hexdigest()
 matches=list(re.finditer(r'(?:public|protected)\s+(?:net\.minecraft\.block\.state\.)?BlockStateContainer\s+createBlockState\(\)\s*\{',s))
 assert len(matches)==len(re.findall(r'\bcreateBlockState\s*\(\s*\)',s)),rel
 for match in matches:
  body=body_at(s,match.end());norm=re.sub(r'\s+',' ',body)
  assert re.match(r'return new (?:BlockStateContainer|ExtendedBlockState)\(this[,)]',norm) or any(re.match(r'return '+k+r'\.'+v+r'\(this[,)]',norm) for k,v in helpers.items()),(rel,norm)
  creates.append(rel+'\t'+norm)
 for match in re.finditer(r'\bsetDefaultState\s*\(',s):
  start=match.end();i=start;depth=1
  while depth:depth+=(s[i]=='(')-(s[i]==')');i+=1
  arg=s[start:i-1];assert re.match(r'(?:this\.)?(?:blockState\.getBaseState\(\)|getDefaultState\(\))\.withProperty\(',arg),(rel,arg)
  setters.append(rel+'\t'+re.sub(r'\s+',' ',arg))
 if p.stem in helpers:
  match=re.search(r'public static BlockStateContainer '+helpers[p.stem]+r'\(Block (\w+)[^{]+\{',s);assert match,p
  body=body_at(s,match.end());assert re.match(r'return new ExtendedBlockState\('+match[1]+r'\s*,',body),p
  helper_rows.append(rel+'\t'+re.sub(r'\s+',' ',match[0]+body+'}'))
assert len(creates)==84 and len(setters)==57 and len(helper_rows)==9
assert sorted(creates)==sorted((H/'source-state-owners.tsv').read_text().splitlines())
if __name__=='__main__':
 assert (H/'source-state-setters.tsv').read_text()=='\n'.join(setters)+'\n';assert (H/'source-state-helpers.tsv').read_text()=='\n'.join(helper_rows)+'\n'
 print('PASS source default-state ownership: 84 create methods, 57 setters, 9 forwarding helpers')
