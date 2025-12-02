import os
import re

dir_path = r"bemba_audio/nkole"

# List of correct labels from labelmap.txt
labelmap = [
    'person','bicycle','car','motorcycle','airplane','bus','train','truck','boat','traffic light','fire hydrant','street sign','stop sign','parking meter','bench','bird','cat','dog','horse','sheep','cow','elephant','bear','zebra','giraffe','hat','backpack','umbrella','shoe','eye glasses','handbag','tie','suitcase','frisbee','skis','snowboard','sports ball','kite','baseball bat','baseball glove','skateboard','surfboard','tennis racket','bottle','plate','wine glass','cup','fork','knife','spoon','bowl','banana','apple','sandwich','orange','broccoli','carrot','hot dog','pizza','donut','cake','chair','couch','potted plant','bed','mirror','dining table','window','desk','toilet','door','tv','laptop','mouse','remote','keyboard','cell phone','microwave','oven','toaster','sink','refrigerator','blender','book','clock','vase','scissors','teddy bear','hair drier','toothbrush','hair brush','banner','blanket','branch','bridge','building-other','bush','cabinet','cage','cardboard','carpet','ceiling-other','ceiling-tile','cloth','clothes','clouds','counter','cupboard','curtain','desk-stuff','dirt','door-stuff','fence','floor-marble','floor-other','floor-stone','floor-tile','floor-wood','flower','fog','food-other','fruit','furniture-other','grass','gravel','ground-other','hill','house','leaves','light','mat','metal','mirror-stuff','moss','mountain','mud','napkin','net','paper','pavement','pillow','plant-other','plastic','platform','playingfield','railing','railroad','river','road','rock','roof','rug','salad','sand','sea','shelf','sky-other','skyscraper','snow','solid-other','stairway','straw','structural-other','table','tent','textile-other','towel','tree','vegetable','wall-brick','wall-concrete','wall-other','wall-panel','wall-stone','wall-tile','wall-wood','water-other','water-drops','window-blind','window-other','wood'
]

def label_to_filename(label):
    base = label.lower().replace('-', '_').replace(' ', '_')
    base = re.sub(r'[^a-z0-9_]', '', base)
    return f"{base}_bemba.m4a"

# Build set of expected lowercase filenames
expected_files = set(label_to_filename(label) for label in labelmap)

# List all files in the directory
files = os.listdir(dir_path)

# List files not in correct lowercase format
not_lowercase = [f for f in files if f != f.lower() or not re.match(r'^[a-z0-9_]+_bemba\.m4a$', f)]
if not_lowercase:
    print("Files NOT in correct lowercase format:")
    for f in not_lowercase:
        print(f"  {f}")
else:
    print("All files are in correct lowercase format.")

# Attempt to automatically rename files to correct format if a match is likely
renamed = 0
for f in not_lowercase:
    # Try to match to a label by normalizing the base name
    base = f.split('_bemba')[0]
    norm_base = re.sub(r'[^a-z0-9]', '', base.lower())
    for label in labelmap:
        norm_label = re.sub(r'[^a-z0-9]', '', label.lower())
        if norm_base == norm_label:
            new_name = label_to_filename(label)
            src = os.path.join(dir_path, f)
            dst = os.path.join(dir_path, new_name)
            if not os.path.exists(dst):
                os.rename(src, dst)
                print(f"Renamed '{f}' -> '{new_name}'")
                renamed += 1
            else:
                print(f"SKIP: '{f}' would overwrite '{new_name}'")
            break

if renamed == 0:
    print("\nNo files were automatically renamed. Manual review may be needed.")
else:
    print(f"\n{renamed} files were automatically renamed to lowercase format.")
