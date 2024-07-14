---
layout: page
title: Publications
permalink: /publications/
---

Showing (<a class="venue-all-toggle">All</a>/<a class="venue-none-toggle">None</a>):
<a class="venue-conference-toggle">Conferences</a> /
<a class="venue-findings-toggle">Findings</a> /
<!-- <a class="venue-journal-toggle">Journals</a> / -->
<a class="venue-workshop-toggle">Workshops</a> /
<a class="venue-thesis-toggle">Theses</a>
<!-- <a class="venue-preprint-toggle">Preprints</a> -->


{% assign cur_year = 2024 %}

{% for yr in (2015..cur_year) reversed %}
  {% capture this_year_count_str %}{% bibliography_count -q @*[year={{ yr }} && tags ~= qasrl] %}{% endcapture %}
  {% assign this_year_count = this_year_count_str | plus: 0 %}
  {% if this_year_count > 0 %}
  <h3>{{ yr }}</h3>
  {% bibliography -q @*[year={{ yr }} && tags ~= qasrl] %}
  {% endif %}
{% endfor %}
