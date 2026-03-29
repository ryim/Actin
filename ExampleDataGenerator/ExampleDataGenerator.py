import json
import random
import uuid
from datetime import datetime, timedelta, UTC

# -----------------------------
# Configuration
# -----------------------------
EXERCISES = [
    "Lat pull down",
    "Hip adduction"
]

ENTRIES_PER_EXERCISE = 24
SETS = 3
USE_KG = True
WEEKS = 12

STARTING_WEIGHTS = {
    "Lat pull down": 45.0,
    "Hip adduction": 40.0
}

WEEKLY_PROGRESS = {
    "Lat pull down": 0.5,
    "Hip adduction": 0.7
}

# -----------------------------
# Step 1: Generate timestamps first
# -----------------------------
def generate_timestamps():
    now = datetime.now(UTC)
    timestamps = []
    for i in range(ENTRIES_PER_EXERCISE):
        days_ago = int((i / ENTRIES_PER_EXERCISE) * (WEEKS * 7))
        ts = now - timedelta(days=days_ago, hours=random.randint(8, 18))
        timestamps.append(ts)
    return sorted(timestamps)

# -----------------------------
# Step 2: Generate entries with monotonic progression
# -----------------------------
def generate_entries_for_exercise(exercise):
    base_weight = STARTING_WEIGHTS[exercise]
    weekly_inc = WEEKLY_PROGRESS[exercise]

    timestamps = generate_timestamps()
    entries = []

    for idx, ts in enumerate(timestamps):
        # Strictly increasing weight
        linear = base_weight + (idx * weekly_inc / 2)

        # Small positive‑leaning variation
        variation = random.uniform(0.0, 0.4)

        weight = round(linear + variation, 1)

        # Reps also trend upward
        base_reps = 8 + idx // 3
        reps = [
            base_reps + random.randint(-1, 1),
            base_reps + random.randint(-1, 1),
            base_reps + random.randint(-1, 1)
        ]

        entry = {
            "id": str(uuid.uuid4()),
            "name": exercise,
            "sets": SETS,
            "reps": reps,
            "weights": [weight] * SETS,
            "useKg": USE_KG,
            "day": ts.day,
            "month": ts.month,
            "year": ts.year,
            "timestamp": ts.isoformat().replace("+00:00", "Z"),
            "workout": ""
        }

        entries.append(entry)

    return entries

# -----------------------------
# Generate all entries
# -----------------------------
all_entries = []
for exercise in EXERCISES:
    all_entries.extend(generate_entries_for_exercise(exercise))

# Sort globally
all_entries.sort(key=lambda x: x["timestamp"])

# -----------------------------
# Output JSON
# -----------------------------
print(json.dumps(all_entries, indent=4))

