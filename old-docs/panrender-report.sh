pandoc -H style.html -sN -r markdown+pipe_tables -w html -o report.html report.md
echo "</body></html>" >> report.html

