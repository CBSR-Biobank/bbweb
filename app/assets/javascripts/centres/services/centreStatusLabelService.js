/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  centreStatusLabelService.$inject = [
    'gettextCatalog',
    'CentreStatus'
  ];

  /**
   * Description
   */
  function centreStatusLabelService(gettextCatalog,
                                    CentreStatus) {
    var labels = {};

    /// centre status
    labels[CentreStatus.DISABLED] = gettextCatalog.getString('Disabled');

    /// centre status
    labels[CentreStatus.ENABLED]  = gettextCatalog.getString('Enabled');

    var service = {
      statusToLabel: statusToLabel
    };
    return service;

    //-------

    function statusToLabel(status) {
      var result = labels[status];
      if (_.isUndefined(result)) {
        throw new Error('invalid status for study: ' + status);
      }
      return result;
    }

  }

  return centreStatusLabelService;
});
