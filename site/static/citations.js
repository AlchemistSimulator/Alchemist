async function fetchCitation(doi, style) {
  const url = `https://doi.org/${doi}`;
  const headers = {
    'Accept': style === 'bibtex' ? 'application/x-bibtex' : `text/x-bibliography; style=${style}`
  };
  const response = await fetch(url, { headers });
  return response.text();
}

function renderCitation(doi, style, elementId) {
  fetchCitation(doi, style).then(citation => {
    const container = document.getElementById(elementId);
    if (style === 'bibtex') {
      const pre = document.createElement('pre');
      pre.textContent = citation.trim()
          .replace(/,\s(\w+\s*=\s*)/g, ',\n  $1')
          .replace(/\s+}$/g, '\n}');
      container.appendChild(pre);
    } else {
      const link = document.createElement('a');
      link.href = `https://doi.org/${doi}`;
      link.innerHTML = citation;
      container.appendChild(link);
    }
  });
}
