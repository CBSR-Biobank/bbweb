/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /**
   * Displays items in a panel list. Can only be used for collections {@link domain.study.Study} and {@link
   * domain.cnetres.Centres}.
   *
   * @return {object} An AngularJS directive.
   */
  var component = {
    template: require('./centresPagedList.html'),
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
    }
  };

  Controller.$inject = [
    '$controller',
    '$q',
    '$log',
    '$scope',
    'Centre',
    'CentreState',
    'CentreCounts',
    '$state',
    'gettextCatalog',
    'NameFilter',
    'StateFilter',
    'centreStateLabelService'
  ];

  /*
   * Controller for this component.
   */
  function Controller($controller,
                      $q,
                      $log,
                      $scope,
                      Centre,
                      CentreState,
                      CentreCounts,
                      $state,
                      gettextCatalog,
                      NameFilter,
                      StateFilter,
                      centreStateLabelService) {
    var vm = this,
        stateData = _.values(CentreState).map(function (state) {
          return { id: state, label: centreStateLabelService.stateToLabelFunc(state) };
        });
    vm.$onInit = onInit;
    vm.filters = {};
    vm.filters[NameFilter.name]  = new NameFilter();
    vm.filters[StateFilter.name] = new StateFilter(true, stateData, 'all');

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

       vm.stateLabelFuncs = {};
       _.values(CentreState).forEach(function (state) {
          vm.stateLabelFuncs[state] = centreStateLabelService.stateToLabelFunc(state);
       });

      CentreCounts.get()
        .then(function (counts) {
          vm.counts = counts;
        })
         .catch(function (error) {
          $log.error(error);
        });
   }

    function getItems(options) {
      return CentreCounts.get()
        .then(function (counts) {
          vm.counts = counts;
          return Centre.list(options);
        });
    }

    function getItemIcon(centre) {
      if (centre.isDisabled()) {
        return 'glyphicon-cog';
      } else if (centre.isEnabled()) {
        return 'glyphicon-ok-circle';
      } else {
        throw new Error('invalid centre state: ' + centre.state);
      }
    }
  }

  return component;
});
