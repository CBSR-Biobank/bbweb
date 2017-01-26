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
    templateUrl: '/assets/javascripts/admin/centres/components/centresPagedList/centresPagedList.html',
    controller: CentresPagedListController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  CentresPagedListController.$inject = [
    '$controller',
    '$scope',
    'Centre',
    'CentreState',
    'CentreCounts',
    'gettextCatalog'
  ];

  /*
   * Controller for this component.
   */
  function CentresPagedListController($controller,
                                      $scope,
                                      Centre,
                                      CentreState,
                                      CentreCounts,
                                      gettextCatalog) {
    var vm = this;

    vm.$onInit     = onInit;
    vm.counts      = {};
    vm.limit       = 5;
    vm.getItems    = Centre.list;
    vm.getItemIcon = getItemIcon;

    vm.stateData = _.map(_.values(CentreState), function (state) {
      return { id: state, label: state.toUpperCase() };
    });

    // initialize this controller's base class
    $controller('PagedListController',
                {
                  vm:             vm,
                  $scope:         $scope,
                  gettextCatalog: gettextCatalog
                });

    //--

    function onInit() {
      CentreCounts.get().then(function (counts) {
        vm.counts = counts;
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
