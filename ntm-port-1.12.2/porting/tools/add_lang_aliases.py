import sys
import os
import zipfile
import shutil

def main():
    if len(sys.argv) < 2:
        print("Usage: add_lang_aliases.py <path_to_jar>")
        sys.exit(1)
        
    jar_path = sys.argv[1]
    if not os.path.isfile(jar_path):
        print(f"Error: JAR not found: {jar_path}")
        sys.exit(1)
        
    tmp_path = jar_path + ".tmp"
    
    with zipfile.ZipFile(jar_path, 'r') as zin:
        entries = zin.infolist()
        all_names = {item.filename for item in entries}
        
        # Identify missing uppercase/lowercase lang aliases
        aliases_to_add = {}
        for item in entries:
            name = item.filename
            if name.startswith("assets/hbm/lang/") and name.endswith(".lang"):
                base = name[len("assets/hbm/lang/"):-len(".lang")]
                if "_" in base:
                    lang, country = base.split("_", 1)
                    upper_name = f"assets/hbm/lang/{lang.lower()}_{country.upper()}.lang"
                    lower_name = f"assets/hbm/lang/{lang.lower()}_{country.lower()}.lang"
                    
                    data = zin.read(name)
                    if upper_name not in all_names and upper_name not in aliases_to_add:
                        aliases_to_add[upper_name] = data
                    if lower_name not in all_names and lower_name not in aliases_to_add:
                        aliases_to_add[lower_name] = data

        if not aliases_to_add:
            print("All lang aliases already exist.")
            return

        print(f"Adding {len(aliases_to_add)} lang aliases: {list(aliases_to_add.keys())}")
        
        with zipfile.ZipFile(tmp_path, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
            for item in entries:
                zout.writestr(item, zin.read(item.filename))
            for alias_name, alias_data in aliases_to_add.items():
                zout.writestr(alias_name, alias_data)
                
    shutil.move(tmp_path, jar_path)
    print("Successfully updated JAR with lang aliases.")

if __name__ == "__main__":
    main()
