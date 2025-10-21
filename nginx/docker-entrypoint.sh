#!/bin/bash

# Start backend in background
cd /app/bin && ./backend --port 8080 --host localhost &
BACKEND_PID=$!

# Start nginx
nginx

# Trap to cleanup on exit
trap "kill $BACKEND_PID; nginx -s stop" EXIT

# Wait
wait
