import sys
lines = [l for l in sys.stdin.read().splitlines() if 'Co-Authored-By: Claude' not in l]
# Strip trailing empty lines
while lines and not lines[-1].strip():
    lines.pop()
print('\n'.join(lines))
