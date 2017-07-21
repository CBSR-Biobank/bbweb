/**
 *
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  breadcrumbService.$inject = ['gettextCatalog'];

  /*
   * Used for creating breadcrumbs.
   *
   * The reason for returning a function that calls gettextCatalog.getString is so that prompts
   * displayed in the client are updated correctly when the user changes the language.
   */
  function breadcrumbService(gettextCatalog) {
    var breadcrumbStateToDisplayFunc = {
      'home':                              function () { return gettextCatalog.getString('Home'); },
      'home.about':                        function () { return gettextCatalog.getString('About'); },
      'home.admin':                        function () { return gettextCatalog.getString('Administration'); },
      'home.admin.studies':                function () { return gettextCatalog.getString('Studies'); },
      'home.admin.centres':                function () { return gettextCatalog.getString('Centres'); },
      'home.admin.users':                  function () { return gettextCatalog.getString('Users'); },
      'home.admin.users.manage':           function () { return gettextCatalog.getString('Manage users'); },
      'home.admin.users.roles':            function () { return gettextCatalog.getString('Roles'); },
      'home.admin.users.memberships':      function () { return gettextCatalog.getString('Memberships'); },
      'home.collection':                   function () { return gettextCatalog.getString('Collection'); },
      'home.collection.study.participantAdd': function () {
        return gettextCatalog.getString('Add participant');
      },
      'home.shipping':      function () { return gettextCatalog.getString('Shipping'); },
      'home.shipping.add':  function () { return gettextCatalog.getString('Add shipment'); }
    };

    var service = {
      forState:         forState,
      forStateWithFunc: forStateWithFunc
    };
    return service;

    //-------

    function forState(stateName) {
      var displayNameFunc = breadcrumbStateToDisplayFunc[stateName];
      if (_.isUndefined(displayNameFunc)) {
        throw new Error('display name function is undefined for state: ' + stateName);
      }
      return { route: stateName, displayNameFn: displayNameFunc };
    }

    function forStateWithFunc(stateName, displayNameFunc) {
      return { route: stateName, displayNameFn: displayNameFunc };
    }

  }

  return breadcrumbService;
});
