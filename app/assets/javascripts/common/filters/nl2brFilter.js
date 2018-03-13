/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

function nl2brFilterFactory() {

  /**
   * An AngualrJS Filter.
   *
   * @memberOf common.filters
   */
  class Nl2brFilter {

    /**
     * Converts new lines in text to HTML line breaks (`<br>`).
     *
     * @param {string} input - the HTML text to convert.
     *
     * @return {string}
     */
    static  filter(input) {
      const span = document.createElement('span');

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

  return Nl2brFilter.filter;
}

export default ngModule => ngModule.filter('nl2br', nl2brFilterFactory)
