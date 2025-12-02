import os

raw_dir = 'app/src/main/res/raw/bemba'
for i, f in enumerate(os.listdir(raw_dir)):
    if i < 50:
        print(i, repr(f))
    else:
        break
