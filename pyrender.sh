cat style.html > fypp.html
#markdown -T -f +toc fypp.md >> fypp.html
markdown_py -x tables -x footnotes -x def_list fypp.md >> fypp.html
echo "</body></html>" >> fypp.html
