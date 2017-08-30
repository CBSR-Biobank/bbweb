/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  userStateLabelService.$inject = [
    'labelService',
    'UserState',
    'gettextCatalog'
  ];

  /**
   * An AngularJS service that converts a UserState to a i18n string that can
   * be displayed to the user.
   *
   * @param {object} UserState - AngularJS constant that enumerates all the user states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   *
   * @return {Service} The AngularJS service.
   */
  function userStateLabelService(labelService,
                                     UserState,
                                     gettextCatalog) {
    var labels = {};

    labels[UserState.REGISTERED] = function () { return gettextCatalog.getString('Registered'); };
    labels[UserState.ACTIVE]     = function () { return gettextCatalog.getString('Active'); };
    labels[UserState.LOCKED]     = function () { return gettextCatalog.getString('Locked'); };

    var service = {
      stateToLabelFunc: stateToLabelFunc
    };
    return service;

    //-------

    function stateToLabelFunc(state) {
      return labelService.getLabel(labels, state);
    }

  }

  return userStateLabelService;
});
