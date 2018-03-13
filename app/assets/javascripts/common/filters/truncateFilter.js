/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

function truncateFilterFactory() {

  /**
   * An AngualrJS Filter.
   *
   * @memberOf common.filters
   */
  class TrunctateFilter {

    /**
     * Truncates a string to a specified length. If `text` exceeds `length`, it is tructated at `length` and
     * string `end` is appended to it.
     *
     * @param {string} text - the string to possibly truncate.
     *
     * @param {int} length=10 - the number of characters to truncate at.
     *
     * @param {string} end='...' - the string to append when `text` is truncated.
     *
     * @return {string}
     */
    static filter(text, length = 10, end = '...') {
      if (! text) {
        return '';
      }

      if (isNaN(length)) {
        length = 10;
      }

      if ((text.length <= length) || (text.length - end.length <= length)) {
        return text;
      }
      return String(text).substring(0, length-end.length) + end;
    }
  }

  return TrunctateFilter.filter;
}

export default ngModule => ngModule.filter('truncate', truncateFilterFactory)
