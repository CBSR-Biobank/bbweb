/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/* @ngInject */
function yesNoFilterFactory(gettextCatalog) {
  return yesNoFilter;

  /**
   * Converts a boolean value to "yes" or "no".
   */
  function yesNoFilter(input) {
    return input ? gettextCatalog.getString('Yes') : gettextCatalog.getString('No');
  }
}

export default ngModule => ngModule.filter('yesNo', yesNoFilterFactory)
