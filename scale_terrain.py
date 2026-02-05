#!/usr/bin/env python3
import json
import os
from pathlib import Path

BIOMES_DIR = Path("plugins/Iris/packs/overworld/biomes")
THRESHOLD = 100  # 이 이상만 조정
OFFSET = 32      # 빼는 양

def scale_max(value):
    """Lower high terrain only"""
    if value < THRESHOLD:
        return value
    return value - OFFSET

def process_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except:
        return False, "read error"

    modified = False

    # Only modify generators array
    if 'generators' in data and isinstance(data['generators'], list):
        for gen in data['generators']:
            if isinstance(gen, dict):
                if 'max' in gen and isinstance(gen['max'], (int, float)):
                    old_val = gen['max']
                    new_val = scale_max(old_val)
                    if old_val != new_val:
                        gen['max'] = new_val
                        modified = True
                        print(f"  max: {old_val} → {new_val}")

                if 'min' in gen and isinstance(gen['min'], (int, float)):
                    old_val = gen['min']
                    new_val = scale_max(old_val)
                    if old_val != new_val:
                        gen['min'] = new_val
                        modified = True
                        print(f"  min: {old_val} → {new_val}")

    if modified:
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=4, ensure_ascii=False)
        return True, "modified"

    return False, "no change"

def main():
    if not BIOMES_DIR.exists():
        print(f"Error: {BIOMES_DIR} not found")
        return

    total = 0
    modified = 0

    for filepath in BIOMES_DIR.rglob("*.json"):
        total += 1
        print(f"Processing: {filepath.relative_to(BIOMES_DIR)}")
        changed, status = process_file(filepath)
        if changed:
            modified += 1

    print(f"\nDone: {modified}/{total} files modified")

if __name__ == "__main__":
    main()
