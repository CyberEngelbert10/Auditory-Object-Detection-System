import os
import re

dir_path = r"bemba_audio/nkole"

labelmap = [
    'person','bicycle','car','motorcycle','airplane','bus','train','truck','boat','traffic light','fire hydrant','street sign','stop sign','parking meter','bench','bird','cat','dog','horse','sheep','cow','elephant','bear','zebra','giraffe','hat','backpack','umbrella','shoe','eye glasses','handbag','tie','suitcase','frisbee','skis','snowboard','sports ball','kite','baseball bat','baseball glove','skateboard','surfboard','tennis racket','bottle','plate','wine glass','cup','fork','knife','spoon','bowl','banana','apple','sandwich','orange','broccoli','carrot','hot dog','pizza','donut','cake','chair','couch','potted plant','bed','mirror','dining table','window','desk','toilet','door','tv','laptop','mouse','remote','keyboard','cell phone','microwave','oven','toaster','sink','refrigerator','blender','book','clock','vase','scissors','teddy bear','hair drier','toothbrush','hair brush','banner','blanket','branch','bridge','building-other','bush','cabinet','cage','cardboard','carpet','ceiling-other','ceiling-tile','cloth','clothes','clouds','counter','cupboard','curtain','desk-stuff','dirt','door-stuff','fence','floor-marble','floor-other','floor-stone','floor-tile','floor-wood','flower','fog','food-other','fruit','furniture-other','grass','gravel','ground-other','hill','house','leaves','light','mat','metal','mirror-stuff','moss','mountain','mud','napkin','net','paper','pavement','pillow','plant-other','plastic','platform','playingfield','railing','railroad','river','road','rock','roof','rug','salad','sand','sea','shelf','sky-other','skyscraper','snow','solid-other','stairway','straw','structural-other','table','tent','textile-other','towel','tree','vegetable','wall-brick','wall-concrete','wall-other','wall-panel','wall-stone','wall-tile','wall-wood','water-other','water-drops','window-blind','window-other','wood'
]

def label_to_filename(label):
    base = label.lower().replace('-', '_').replace(' ', '_')
    base = re.sub(r'[^a-z0-9_]', '', base)
    return f"{base}_bemba.m4a"

expected_files = set(label_to_filename(label) for label in labelmap)
files = set(f for f in os.listdir(dir_path) if re.match(r'^[a-z0-9_]+_bemba\.m4a$', f))
missing = sorted(expected_files - files)
print("Missing files:")
for f in missing:
    print(f"  {f}")
print(f"\nTotal missing: {len(missing)}")
