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
    templateUrl: '/assets/javascripts/admin/studies/components/studiesPagedList/studiesPagedList.html',
    controller: StudiesPagedListController,
    controllerAs: 'vm',
    bindings: {
    }
  };

  StudiesPagedListController.$inject = [
    '$controller',
    '$scope',
    '$state',
    'Study',
    'StudyState',
    'StudyCounts',
    'gettextCatalog'
  ];

  /*
   * Controller for this component.
   */
  function StudiesPagedListController($controller,
                                      $scope,
                                      $state,
                                      Study,
                                      StudyState,
                                      StudyCounts,
                                      gettextCatalog) {
    var vm = this;
    vm.$onInit     = onInit;

    //--

    function onInit() {
      vm.counts      = {};
      vm.limit       = 5;
      vm.getItems    = getItems;
      vm.getItemIcon = getItemIcon;

      vm.stateData = _.map(_.values(StudyState), function (state) {
        return { id: state, label: state.toUpperCase() };
      });

      // initialize this controller's base class
      $controller('PagedListController', {
        vm:             vm,
        gettextCatalog: gettextCatalog
      });

      return StudyCounts.get()
        .then(function (counts) {
          vm.counts = counts;
        })
        .catch(handleUnauthorized);
    }

    function getItems(options) {
      // KLUDGE: for now, fix after Entity Pagers have been implemented
      return StudyCounts.get()
        .then(function (counts) {
          vm.counts = counts;
          return Study.list(options);
        })
        .catch(handleUnauthorized);
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

    function handleUnauthorized(error) {
      if (error.status && (error.status === 401)) {
        $state.go('home.users.login', {}, { reload: true });
      }
      return null;
    }
  }

  return component;
});
