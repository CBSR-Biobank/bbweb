/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   * Displays the study name and description and allows the user to change the state of the study.
   */
  function studySummaryDirective() {
    var directive = {
      restrict: 'E',
      scope: {},
      bindToController: {
        study: '='
      },
      templateUrl : '/assets/javascripts/admin/studies/directives/studySummary/studySummary.html',
      controller: StudySummaryCtrl,
      controllerAs: 'vm'
    };

    return directive;
  }

  StudySummaryCtrl.$inject = [
    '$scope',
    '$state',
    'gettextCatalog',
    'modalService',
    'modalInput',
    'notificationsService',
    'CollectionEventType'
  ];

  function StudySummaryCtrl($scope,
                            $state,
                            gettextCatalog,
                            modalService,
                            modalInput,
                            notificationsService,
                            CollectionEventType) {

    var vm = this;

    vm.descriptionToggleLength = 100;
    vm.changeState             = changeState;
    vm.editName                = editName;
    vm.editDescription         = editDescription;

    init();

    //--

    function init() {
      // updates the selected tab in 'studyViewDirective' which is the parent directive
      $scope.$emit('study-view', 'study-summary-selected');

      CollectionEventType.list(vm.study.id).then(function (ceTypes) {
        var specimenSpecs = _.flatMap(ceTypes, function(ceType) { return ceType.specimenSpecs; });
        vm.hasSpecimenSpecs = (specimenSpecs.length > 0);
      });
    }

    function changeState(stateAction) {
      var body;

      switch (stateAction) {
      case 'disable':
        body = gettextCatalog.getString('Are you sure you want to disable study {{name}}',
                                        { name: vm.study.name });
        break;
      case 'enable':
         body = gettextCatalog.getString('Are you sure you want to enable study {{name}}',
                                         { name: vm.study.name });
        break;
      case 'retire':
        body = gettextCatalog.getString('Are you sure you want to retire study {{name}}',
                                         { name: vm.study.name });
        break;
      case 'unretire':
        body = gettextCatalog.getString('Are you sure you want to unretire study {{name}}',
                                         { name: vm.study.name });
        break;
      default:
        throw new Error('invalid state: ' + stateAction);
      }

      body += stateAction + ' study ' + vm.study.name + '?';

      modalService.modalOkCancel(gettextCatalog.getString('Confirm study state change'), body)
        .then(function () {
          return vm.study[stateAction]();
        }).then(function (study) {
          vm.study = study;
          notificationsService.success('The study\'s state has been updated.', null, 2000);
        });
    }

    function postUpdate(message, title, timeout) {
      timeout = timeout || 1500;
      return function (study) {
        vm.study = study;
        notificationsService.success(message, title, timeout);
      };
    }

    function editName() {
      modalInput.text(gettextCatalog.getString('Edit name'),
                      gettextCatalog.getString('Name'),
                      vm.study.name,
                      { required: true, minLength: 2 }).result
        .then(function (name) {
          vm.study.updateName(name)
            .then(postUpdate(gettextCatalog.getString('Name changed successfully.'),
                             gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
        });
    }

    function editDescription() {
      modalInput.textArea(gettextCatalog.getString('Edit description'),
                          gettextCatalog.getString('Description'),
                          vm.study.description).result
        .then(function (description) {
          vm.study.updateDescription(description)
            .then(postUpdate(gettextCatalog.getString('Description changed successfully.'),
                             gettextCatalog.getString('Change successful')))
            .catch(notificationsService.updateError);
        });
    }
  }

  return studySummaryDirective;

});
