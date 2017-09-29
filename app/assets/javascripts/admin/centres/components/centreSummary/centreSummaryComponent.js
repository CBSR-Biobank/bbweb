/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Displays the centre administrtion page, with a number of tabs. Each tab displays the configuration
   * for a different aspect of the centre.
   */
  var component = {
    template: require('./centreSummary.html'),
    controller: CentreSummaryController,
    controllerAs: 'vm',
    bindings: {
      centre: '<'
    }
  };

  CentreSummaryController.$inject = [
    '$scope',
    'gettextCatalog',
    'modalService',
    'modalInput',
    'notificationsService',
    'centreStateLabelService'
  ];

  /*
   * Controller for this component.
   */
  function CentreSummaryController($scope,
                                   gettextCatalog,
                                   modalService,
                                   modalInput,
                                   notificationsService,
                                   centreStateLabelService) {
    var vm = this;
    vm.$onInit = onInit;

    //----

    function onInit() {
      vm.descriptionToggleControl = {}; // for truncateToggle directive
      vm.descriptionToggleState   = true;
      vm.descriptionToggleLength  = 100;

      vm.changeState     = changeState;
      vm.editName        = editName;
      vm.editDescription = editDescription;

      vm.stateLabelFunc  = centreStateLabelService.stateToLabelFunc(vm.centre.state);

      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

    function changeState(studyState) {
      var changeStateFn,
          stateChangeMsg;

      if (studyState === 'enable') {
        changeStateFn = vm.centre.enable;
        stateChangeMsg = gettextCatalog.getString('Are you sure you want to enable centre {{name}}?',
                                                   { name: vm.centre.name });
      } else if (studyState === 'disable') {
        changeStateFn = vm.centre.disable;
        stateChangeMsg = gettextCatalog.getString('Are you sure you want to disable centre {{name}}?',
                                                   { name: vm.centre.name });
      } else {
        throw new Error('invalid state: ' + studyState);
      }

      modalService.modalOkCancel(gettextCatalog.getString('Confirm state change on centre'),
                                 stateChangeMsg)
        .then(function () {
          changeStateFn.call(vm.centre)
            .then(function (centre) {
              vm.centre = centre;
            });
      });
    }

    function postUpdate(message, title, timeout) {
      timeout = timeout || 1500;
      return function (centre) {
        vm.centre = centre;
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text(gettextCatalog.getString('Edit name'),
                      gettextCatalog.getString('Name'),
                      vm.centre.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.centre.updateName(name)
            .then(function (centre) {
              $scope.$emit('centre-name-changed', centre);
              postUpdate(gettextCatalog.getString('Name changed successfully.'),
                         gettextCatalog.getString('Change successful'))(centre);
            })
            .catch(notificationsService.updateError);
        });
    }

    function editDescription() {
      modalInput.textArea(gettextCatalog.getString('Edit description'),
                          gettextCatalog.getString('Description'),
                          vm.centre.description).result
        .then(function (description) {
          vm.centre.updateDescription(description)
            .then(postUpdate(gettextCatalog.getString('Description changed successfully.'),
                             gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

  }

  return component;
});
