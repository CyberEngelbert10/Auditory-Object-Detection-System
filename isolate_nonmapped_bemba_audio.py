import os
import csv
import shutil

raw_dir = 'app/src/main/res/raw/bemba'
map_file = 'bemba_audio_mapping.csv'
extra_dir = os.path.join(raw_dir, 'extras')

os.makedirs(extra_dir, exist_ok=True)

# Read mapping file, get filenames (3rd column 'filename' or the 3rd column name present)
mapped_filenames = set()
with open(map_file, encoding='utf-8') as f:
    reader = csv.reader(f)
    header = next(reader)
    # Determine the filename column index (should be 2 or 3)
    try:
        # If header contains 'filename' or 'filename_noext'
        if 'filename' in header:
            fn_idx = header.index('filename')
        elif 'filename_noext' in header:
            fn_idx = header.index('filename_noext')
        else:
            fn_idx = 2
    except Exception:
        fn_idx = 2

    for r in reader:
        # If the column contains an extension or not, ensure it ends with _bemba.m4a
        fn = r[fn_idx].strip()
        if not fn.lower().endswith('.m4a'):
            if not fn.endswith('_bemba'):
                fn = fn + '_bemba.m4a'
            else:
                fn = fn + '.m4a'
        mapped_filenames.add(fn)

# Move files that are not mapped
moved = []
for f in os.listdir(raw_dir):
    fpath = os.path.join(raw_dir, f)
    if os.path.isdir(fpath):
        continue
    if f not in mapped_filenames:
        # Move to extras
        dst = os.path.join(extra_dir, f)
        shutil.move(fpath, dst)
        moved.append(f)
        print(f"Moved non-mapped: {f} -> {dst}")

if not moved:
    print("No non-mapped files found to move.")
else:
    print(f"Moved {len(moved)} non-mapped files to {extra_dir}")
