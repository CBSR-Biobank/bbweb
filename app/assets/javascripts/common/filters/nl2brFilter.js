/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  nl2brFilterFactory.$inject = [];

  function nl2brFilterFactory() {
    var span = document.createElement('span');
    return nl2brFilter;

    /**
     * Description
     */
    function nl2brFilter(input) {
      if (!input) { return input; }
      var lines = input.split('\n');

      for (var i = 0; i < lines.length; i++) {
        span.innerText = lines[i];
        span.textContent = lines[i];  //for Firefox
        lines[i] = span.innerHTML;
      }
      return lines.join('<br />');

    }
  }

  return nl2brFilterFactory;

});
