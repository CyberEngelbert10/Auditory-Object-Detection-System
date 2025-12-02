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

# Build a mapping from normalized base to correct filename
labelmap_norm = {re.sub(r'[^a-z0-9]', '', label.lower()): label_to_filename(label) for label in labelmap}

files = os.listdir(dir_path)
renamed = 0
for f in files:
    if any(c.isupper() for c in f):
        base = f.split('_bemba')[0]
        norm_base = re.sub(r'[^a-z0-9]', '', base.lower())
        if norm_base in labelmap_norm:
            correct_name = labelmap_norm[norm_base]
            src = os.path.join(dir_path, f)
            dst = os.path.join(dir_path, correct_name)
            os.replace(src, dst)  # Overwrite if exists
            print(f"Renamed '{f}' -> '{correct_name}' (overwritten if existed)")
            renamed += 1
        else:
            print(f"Could not match '{f}' to any label. Please review manually.")

print(f"\nDone. {renamed} files were force-renamed to the correct lowercase/underscore format.")
