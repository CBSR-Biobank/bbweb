/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  yesNoFilterFactory.$inject = ['gettextCatalog'];

  function yesNoFilterFactory(gettextCatalog) {
    return yesNoFilter;

    /**
     * Converts a boolean value to "yes" or "no".
     */
    function yesNoFilter(input) {
      return input ? gettextCatalog.getString('Yes') : gettextCatalog.getString('No');
    }
  }

  return yesNoFilterFactory;
});
