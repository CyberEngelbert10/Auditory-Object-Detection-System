import os
import re

dir_path = r"bemba_audio/nkole"

# List all files in the directory
files = os.listdir(dir_path)

# Build a set of all lowercase-format files
lowercase_files = set(f for f in files if f == f.lower() and re.match(r'^[a-z0-9_]+_bemba\.m4a$', f))

deleted = 0
for f in files:
    if f not in lowercase_files:
        # Try to find the corresponding lowercase version
        base = f.split('_bemba')[0]
        norm_base = re.sub(r'[^a-z0-9]', '', base.lower())
        for lc in lowercase_files:
            lc_base = lc.split('_bemba')[0]
            if re.sub(r'[^a-z0-9]', '', lc_base) == norm_base:
                os.remove(os.path.join(dir_path, f))
                print(f"Deleted: {f}")
                deleted += 1
                break

print(f"\nDone. Deleted {deleted} uppercase/oddly-named files where lowercase version exists.")
