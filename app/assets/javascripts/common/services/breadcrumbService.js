/**
 *
 */

import _ from 'lodash'

/*
 * Used for creating breadcrumbs.
 *
 * The reason for returning a function that calls gettextCatalog.getString is so that prompts
 * displayed in the client are updated correctly when the user changes the language.
 */
/* @ngInject */
function breadcrumbService(gettextCatalog) {
  var addLabelFunc = () =>  gettextCatalog.getString('Add'),
      breadcrumbStateToDisplayFunc = {
        'home':                                 () => gettextCatalog.getString('Home'),
        'home.about':                           () => gettextCatalog.getString('About'),
        'home.contact':                         () => gettextCatalog.getString('Contact'),
        'home.admin':                           () => gettextCatalog.getString('Administration'),
        'home.admin.studies':                   () => gettextCatalog.getString('Studies'),
        'home.admin.centres':                   () => gettextCatalog.getString('Centres'),
        'home.admin.users':                     () => gettextCatalog.getString('Users'),
        'home.admin.users.manage':              () => gettextCatalog.getString('Manage users'),
        'home.admin.users.roles':               () => gettextCatalog.getString('Roles'),
        'home.admin.users.memberships':         () => gettextCatalog.getString('Memberships'),
        'home.collection':                      () => gettextCatalog.getString('Collection'),
        'home.shipping':                        () => gettextCatalog.getString('Shipping'),
        'home.shipping.add':                    () => gettextCatalog.getString('Add shipment'),
        'home.collection.study.participantAdd': () => gettextCatalog.getString('Add participant'),
        'home.admin.centres.add':               addLabelFunc,
        'home.admin.studies.add':               addLabelFunc,
        'home.admin.users.memberships.add':     addLabelFunc
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

export default ngModule => ngModule.service('breadcrumbService', breadcrumbService)
