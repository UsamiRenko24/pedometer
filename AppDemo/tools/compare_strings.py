import re
from pathlib import Path

base = Path(r"D:\Project\AppDemo\app\src\main\res")
default = base / "values" / "strings.xml"


def parse_keys(path):
    text = path.read_text(encoding="utf-8")
    keys = set()
    for m in re.finditer(r'<string\s+name="([^"]+)"', text):
        keys.add(m.group(1))
    for m in re.finditer(r'<string-array\s+name="([^"]+)"', text):
        keys.add(m.group(1) + "[]")
    return keys


def parse_string_entries(path):
    text = path.read_text(encoding="utf-8")
    entries = {}
    for m in re.finditer(r'<string\s+name="([^"]+)"[^>]*>(.*?)</string>', text, re.DOTALL):
        entries[m.group(1)] = m.group(2).strip()
    return entries


default_keys = parse_keys(default)
default_entries = parse_string_entries(default)

locales = sorted(
    p.parent.name for p in base.glob("values*/strings.xml") if p.parent.name != "values"
)

print("Default keys:", len(default_keys))
missing_by_locale = {}
for loc in locales:
    p = base / loc / "strings.xml"
    keys = parse_keys(p)
    missing = sorted(default_keys - keys)
    missing_by_locale[loc] = missing
    print(f"{loc}: {len(keys)} keys, missing {len(missing)}")

all_missing = sorted(set().union(*missing_by_locale.values()))
print("\nUnique missing keys:", len(all_missing))
for k in all_missing:
    print(k)

# export missing list for zh
if missing_by_locale.get("values-zh"):
    print("\n--- values-zh missing ---")
    for k in missing_by_locale["values-zh"]:
        print(k, "=>", default_entries.get(k.replace("[]", ""), "?")[:60])
