/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  centreStatusLabelService.$inject = [
    'gettext',
    'CentreStatus'
  ];

  /**
   * Description
   */
  function centreStatusLabelService(gettext,
                                    CentreStatus) {
    var labels = {};

    labels[CentreStatus.DISABLED] = gettext('Disabled');
    labels[CentreStatus.ENABLED]  = gettext('Enabled');

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
