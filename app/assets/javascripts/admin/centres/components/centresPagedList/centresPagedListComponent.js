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
    '$q',
    '$scope',
    'Centre',
    'CentreState',
    'CentreCounts',
    '$state',
    'gettextCatalog'
  ];

  /*
   * Controller for this component.
   */
  function CentresPagedListController($controller,
                                      $q,
                                      $scope,
                                      Centre,
                                      CentreState,
                                      CentreCounts,
                                      $state,
                                      gettextCatalog) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.counts      = {};
      vm.limit       = 5;
      vm.getItems    = getItems;
      vm.getItemIcon = getItemIcon;

      vm.stateData = _.map(_.values(CentreState), function (state) {
        return { id: state, label: state.toUpperCase() };
      });

      // initialize this controller's base class
      $controller('PagedListController', {
        vm:             vm,
        gettextCatalog: gettextCatalog
      });

      CentreCounts.get()
        .then(function (counts) {
          vm.counts = counts;
        })
        .catch(handleUnauthorized);
    }

    function getItems(options) {
      return CentreCounts.get()
        .then(function (counts) {
          vm.counts = counts;
          return Centre.list(options);
        })
        .catch(handleUnauthorized);
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

    function handleUnauthorized(error) {
      if (error.status && (error.status === 401)) {
        $state.go('home.users.login', {}, { reload: true });
      }
      return null;
    }
  }

  return component;
});
