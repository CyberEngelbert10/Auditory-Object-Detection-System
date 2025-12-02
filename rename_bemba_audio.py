import os
import re

# Directory containing the audio files
dir_path = r"bemba_audio/nkole"

# List of correct labels from labelmap.txt
labelmap = [
    'person','bicycle','car','motorcycle','airplane','bus','train','truck','boat','traffic light','fire hydrant','street sign','stop sign','parking meter','bench','bird','cat','dog','horse','sheep','cow','elephant','bear','zebra','giraffe','hat','backpack','umbrella','shoe','eye glasses','handbag','tie','suitcase','frisbee','skis','snowboard','sports ball','kite','baseball bat','baseball glove','skateboard','surfboard','tennis racket','bottle','plate','wine glass','cup','fork','knife','spoon','bowl','banana','apple','sandwich','orange','broccoli','carrot','hot dog','pizza','donut','cake','chair','couch','potted plant','bed','mirror','dining table','window','desk','toilet','door','tv','laptop','mouse','remote','keyboard','cell phone','microwave','oven','toaster','sink','refrigerator','blender','book','clock','vase','scissors','teddy bear','hair drier','toothbrush','hair brush','banner','blanket','branch','bridge','building-other','bush','cabinet','cage','cardboard','carpet','ceiling-other','ceiling-tile','cloth','clothes','clouds','counter','cupboard','curtain','desk-stuff','dirt','door-stuff','fence','floor-marble','floor-other','floor-stone','floor-tile','floor-wood','flower','fog','food-other','fruit','furniture-other','grass','gravel','ground-other','hill','house','leaves','light','mat','metal','mirror-stuff','moss','mountain','mud','napkin','net','paper','pavement','pillow','plant-other','plastic','platform','playingfield','railing','railroad','river','road','rock','roof','rug','salad','sand','sea','shelf','sky-other','skyscraper','snow','solid-other','stairway','straw','structural-other','table','tent','textile-other','towel','tree','vegetable','wall-brick','wall-concrete','wall-other','wall-panel','wall-stone','wall-tile','wall-wood','water-other','water-drops','window-blind','window-other','wood'
]


def label_to_filename(label):
    # Convert label to lowercase, replace spaces/hyphens with underscores, remove non-alphanum/underscore
    base = label.lower().replace('-', '_').replace(' ', '_')
    base = re.sub(r'[^a-z0-9_]', '', base)
    return f"{base}_bemba.m4a"

def find_best_match(label, files):
    # Only match files where the base name matches the full label (ignoring case, spaces, hyphens, underscores)
    norm_label = normalize_label(label)
    for f in files:
        base = f.split('_bemba')[0]
        if normalize_label(base) == norm_label:
            return f
    return None
def normalize_label(label):
    # Lowercase, remove non-alphanum, replace spaces/hyphens/underscores with nothing
    return re.sub(r'[^a-z0-9]', '', label.lower())

# Build a mapping from label to expected filename
expected_filenames = {label: label_to_filename(label) for label in labelmap}

# List all files in the directory
files = os.listdir(dir_path)

# Try to match and rename
collisions = []
for label, expected in expected_filenames.items():
    found = find_best_match(label, files)
    if found and found != expected:
        src = os.path.join(dir_path, found)
        dst = os.path.join(dir_path, expected)
        if os.path.exists(dst):
            print(f"COLLISION: '{found}' would overwrite '{expected}'. Skipping.")
            collisions.append((found, expected))
            continue
        print(f"Renaming '{found}' -> '{expected}'")
        os.rename(src, dst)
        files.remove(found)
        files.append(expected)
    elif not found:
        print(f"Missing audio for: {label}")
    # else: already correct

if collisions:
    print("\n--- COLLISIONS LOG ---")
    for src, dst in collisions:
        print(f"'{src}' would overwrite '{dst}'")
    print("--- END COLLISIONS LOG ---\n")

print("\nDone. All files are now consistently named.")
