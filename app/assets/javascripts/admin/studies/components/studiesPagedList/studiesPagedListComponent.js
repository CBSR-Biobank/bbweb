/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /**
   * Displays studies in a panel list.
   *
   * @return {object} An AngularJS component.
   */
  var component = {
    template: require('./studiesPagedList.html'),
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
    }
  };

  Controller.$inject = [
    '$controller',
    '$log',
    '$scope',
    '$state',
    'Study',
    'StudyState',
    'StudyCounts',
    'gettextCatalog',
    'NameFilter',
    'StateFilter',
    'studyStateLabelService'
  ];

  /*
   * Controller for this component.
   */
  function Controller($controller,
                      $log,
                      $scope,
                      $state,
                      Study,
                      StudyState,
                      StudyCounts,
                      gettextCatalog,
                      NameFilter,
                      StateFilter,
                      studyStateLabelService) {
    var vm = this,
        stateData = _.values(StudyState).map(function (state) {
          return { id: state, label: studyStateLabelService.stateToLabelFunc(state) };
        });
    vm.$onInit = onInit;
    vm.filters = {};
    vm.filters[NameFilter.name]  = new NameFilter();
    vm.filters[StateFilter.name] = new StateFilter(true, stateData, 'all');

    vm.stateLabelFuncs = {};
    _.values(StudyState).forEach(function (state) {
      vm.stateLabelFuncs[state] = studyStateLabelService.stateToLabelFunc(state);
    });

    //--

    function onInit() {
      vm.counts      = {};
      vm.limit       = 5;
      vm.getItems    = getItems;
      vm.getItemIcon = getItemIcon;

      // initialize this controller's base class
      $controller('PagedListController', {
        vm:             vm,
        $log:           $log,
        $state:         $state,
        gettextCatalog: gettextCatalog
      });

      return StudyCounts.get()
        .then(function (counts) {
          vm.counts = counts;
        })
        .catch(function (error) {
          $log.error(error);
        });
    }

    function getItems(options) {
      // KLUDGE: for now, fix after Entity Pagers have been implemented
      return StudyCounts.get()
        .then(function (counts) {
          vm.counts = counts;
          return Study.list(options);
        });
    }

    function getItemIcon(study) {
      if (study.isDisabled()) {
        return 'glyphicon-cog';
      } else if (study.isEnabled()) {
        return 'glyphicon-ok-circle';
      } else if (study.isRetired()) {
        return 'glyphicon-remove-sign';
      } else {
        throw new Error('invalid study state: ' + study.state);
      }
    }
  }

  return component;
});
