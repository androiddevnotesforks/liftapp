package com.patrykandpatryk.liftapp.domain.workout

interface WorkoutRepository :
    GetWorkoutContract, UpsertWorkoutGoalContract, UpsertExerciseSetContract, UpdateWorkoutContract
