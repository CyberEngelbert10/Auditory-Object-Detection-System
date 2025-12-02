import os
import shutil

bemba_dir = 'app/src/main/res/raw/bemba'
raw_dir = 'app/src/main/res/raw'

if not os.path.exists(bemba_dir):
    print(f"{bemba_dir} does not exist. Nothing to move.")
    exit(0)

files = [f for f in os.listdir(bemba_dir) if os.path.isfile(os.path.join(bemba_dir, f))]

for f in files:
    src = os.path.join(bemba_dir, f)
    dst = os.path.join(raw_dir, f)
    # overwrite if exists
    if os.path.exists(dst):
        print(f"Overwriting existing {dst}")
        os.remove(dst)
    shutil.move(src, dst)
    print(f"Moved {f} to {raw_dir}")

# Remove the now-empty bemba folder if empty
try:
    os.rmdir(bemba_dir)
    print(f"Removed empty directory {bemba_dir}")
except Exception as e:
    print(f"Could not remove {bemba_dir}: {e}")

print('Done.')
