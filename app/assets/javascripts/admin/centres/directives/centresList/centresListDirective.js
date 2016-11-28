/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   *
   */
  function centresListDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
      },
      templateUrl : '/assets/javascripts/admin/centres/directives/centresList/centresList.html',
      controller: CentresListCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  CentresListCtrl.$inject = [
    '$scope',
    'Centre',
    'CentreState',
    'CentreCounts'
  ];

  function CentresListCtrl($scope, Centre, CentreState, CentreCounts) {
    var vm = this;

    vm.centreCounts   = {};
    vm.limit          = 5;
    vm.updateCentres  = Centre.list;
    vm.getCentreIcon  = getCentreIcon;
    vm.possibleStates = [{ id: 'all', label: 'All' }];

    _.each(_.values(CentreState), function(state) {
      vm.possibleStates.push({ id: state, label: state.toUpperCase() });
    });

    init();

    //--

    function init() {
      CentreCounts.get().then(function (counts) {
        vm.centreCounts = counts;
      });
    }

    function getCentreIcon(centre) {
      if (centre.isDisabled()) {
        return 'glyphicon-cog';
      }
      return 'glyphicon-ok-circle';
    }
  }

  return centresListDirective;
});
