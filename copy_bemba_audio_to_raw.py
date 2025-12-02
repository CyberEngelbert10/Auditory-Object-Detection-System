import os
import shutil

src_dir = r"bemba_audio/nkole"
dst_dir = r"app/src/main/res/raw/bemba"

os.makedirs(dst_dir, exist_ok=True)
files = [f for f in os.listdir(src_dir) if f.lower().endswith('.m4a') or f.lower().endswith('.mp3') or f.lower().endswith('.wav')]

for f in files:
    src = os.path.join(src_dir, f)
    dst = os.path.join(dst_dir, f)
    shutil.copy2(src, dst)
    print(f"Copied: {f} -> {dst}")

print(f"\nDone. {len(files)} files copied to {dst_dir}.")
