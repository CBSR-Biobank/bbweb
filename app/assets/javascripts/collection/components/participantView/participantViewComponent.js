/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    template: require('./participantView.html'),
    controller: ParticipantViewController,
    controllerAs: 'vm',
    bindings: {
      study:       '<',
      participant: '<'
    }
  };

  ParticipantViewController.$inject = [
    '$window',
    '$scope',
    '$state',
    'gettextCatalog',
    'breadcrumbService'
  ];

  /*
   * Controller for this component.
   */
  function ParticipantViewController($window,
                                     $scope,
                                     $state,
                                     gettextCatalog,
                                     breadcrumbService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.breadcrumbs = [
        breadcrumbService.forState('home'),
        breadcrumbService.forState('home.collection'),
        breadcrumbService.forStateWithFunc('home.collection.study', function () {
          return vm.study.name;
        }),
        breadcrumbService.forStateWithFunc('home.collection.study.participant', function () {
          return gettextCatalog.getString('Participant {{uniqueId}}',
                                          { uniqueId: vm.participant.uniqueId });
        })
      ];

      vm.tabs = [
        {
          heading: gettextCatalog.getString('Summary'),
          sref: 'home.collection.study.participant.summary',
          active: false
        },
        {
          heading: gettextCatalog.getString('Collection'),
          sref: 'home.collection.study.participant.cevents',
          active: false
        }
      ];
      _.each(vm.tabs, function (tab, index) {
        tab.active = ($state.current.name.indexOf(tab.sref) >= 0);
        if (tab.active) {
          vm.active = index;
        }
      });
    }

  }

  return component;
});
