/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function yesNoFilterFactory(gettextCatalog) {

  /**
   * An AngualrJS Filter.
   *
   * @memberOf common.filters
   */
  class YesNoFilter {

    /**
     * Converts a Boolean value to either `Yes` or `No`.
     *
     * @param {boolean} input - the value to convert.
     *
     * @return {string}
     */
    static filter(input) {
      return input ? gettextCatalog.getString('Yes') : gettextCatalog.getString('No');
    }
  }

  return YesNoFilter.filter;
}

export default ngModule => ngModule.filter('yesNo', yesNoFilterFactory)
