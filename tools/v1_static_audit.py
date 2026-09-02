#!/usr/bin/env python3
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "app/src/main/java/com/rizwan/tasbeehcounter"
RES = ROOT / "app/src/main/res"
checks = []

def check(name, condition, detail=""):
    checks.append((name, bool(condition), detail))

# XML well-formedness
xml_ok = True
xml_error = ""
for path in list(RES.rglob("*.xml")) + [ROOT / "app/src/main/AndroidManifest.xml"]:
    try:
        ET.parse(path)
    except Exception as exc:
        xml_ok = False
        xml_error = f"{path.name}: {exc}"
        break
check("تمام XML فائلیں درست parse ہوں", xml_ok, xml_error)

all_source = "\n".join(p.read_text(encoding="utf-8") for p in JAVA.glob("*.java"))
all_layouts = "\n".join(p.read_text(encoding="utf-8") for p in (RES / "layout").glob("*.xml"))
manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
gradle = (ROOT / "app/build.gradle").read_text(encoding="utf-8")
main = (JAVA / "MainActivity.java").read_text(encoding="utf-8")
last = (JAVA / "LastSessionStore.java").read_text(encoding="utf-8")
tasbeeh = (JAVA / "TasbeehActivity.java").read_text(encoding="utf-8")
history = (JAVA / "DhikrHistoryStore.java").read_text(encoding="utf-8")
catalog = (JAVA / "DhikrCatalog.java").read_text(encoding="utf-8")

check("Version 1.0.1 date-fix مقرر ہے", "versionCode 19" in gradle and "1.0.1-date-fix" in gradle)
check("Explicit sans font باقی نہیں", 'fontFamily="sans"' not in all_layouts and "Typeface.SANS_SERIF" not in all_source)
check("Dedicated incomplete screen manifest میں ہے", "IncompleteSessionsActivity" in manifest)
check("Dedicated history screen manifest میں ہے", "HistoryActivity" in manifest)
check("Resume card popup کے بجائے Activity کھولتا ہے", "IncompleteSessionsActivity.class" in main)
check("ہر ذکر کے IDs الگ محفوظ ہوتے ہیں", "resume_session_ids_v3" in last and "ids.add(0, mode)" in last)
check("History تاریخ اور mode کے لحاظ سے محفوظ ہے", "history_modes_v1_" in history and "base(date, mode)" in history)
check("نئے دن کا session date محفوظ ہے", "EXTRA_SESSION_DATE" in all_source and "sessionDate" in last)
check("پانچ تہلیلات کے پانچ targets ایک ایک ہیں", "new int[]{1, 1, 1, 1, 1}" in catalog)
check("لڑی target الگ محفوظ ہے", "sequenceRoundTarget" in tasbeeh and "completedRounds" in tasbeeh)
check("تمام dialogs پر UrduFont لگایا گیا ہے", all_source.count("new AlertDialog.Builder") == all_source.count("setOnShowListener"))

failed = [c for c in checks if not c[1]]
for name, ok, detail in checks:
    print(("PASS" if ok else "FAIL") + " — " + name + ((" — " + detail) if detail else ""))
print(f"\nنتیجہ: {len(checks)-len(failed)}/{len(checks)} checks pass")
sys.exit(1 if failed else 0)
