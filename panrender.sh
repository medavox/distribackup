pandoc -H style.html -sN -r markdown+pipe_tables -w html -o fypandoc.html fypp.md
echo "</body></html>" >> fypandoc.html

