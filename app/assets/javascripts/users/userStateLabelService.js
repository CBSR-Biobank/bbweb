/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  shipmentStateLabelService.$inject = [
    'UserState',
    'gettextCatalog'
  ];

  /**
   * An AngularJS service that converts a UserState to a i18n string that can
   * be displayed to the user.
   *
   * @param {object} UserState - AngularJS constant that enumerates all the shipment states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   *
   * @return {Service} The AngularJS service.
   */
  function shipmentStateLabelService(UserState, gettextCatalog) {
    var labels = {};

    labels[UserState.REGISTERED] = gettextCatalog.getString('Registered');
    labels[UserState.ACTIVE]     = gettextCatalog.getString('Active');
    labels[UserState.LOCKED]     = gettextCatalog.getString('Locked');

    var service = {
      stateToLabel: stateToLabel
    };
    return service;

    //-------

    function stateToLabel(state) {
      var result = labels[state];
      if (_.isUndefined(result)) {
        throw new Error('invalid user state: ' + state);
      }
      return result;
    }

  }

  return shipmentStateLabelService;
});
