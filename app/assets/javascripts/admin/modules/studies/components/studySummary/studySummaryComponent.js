/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/**
 * Displays the study name and description and allows the user to change the state of the study.
 *
 * Emits event 'study-name-changed' when the user updates the name on the study.
 */
var component = {
  template: require('./studySummary.html'),
  controller: StudySummaryController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function StudySummaryController($scope,
                                $state,
                                gettextCatalog,
                                modalService,
                                modalInput,
                                notificationsService,
                                studyStateLabelService) {

  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.descriptionToggleLength = 100;
    vm.isEnableAllowed         = false;
    vm.changeState             = changeState;
    vm.editName                = editName;
    vm.editDescription         = editDescription;

    // updates the selected tab in 'studyViewDirective' which is the parent directive
    $scope.$emit('tabbed-page-update', 'tab-selected');

    // replace this with an API call
    vm.study.isEnableAllowed().then(function (enableAllowed) {
      vm.isEnableAllowed = enableAllowed;
    });

    vm.stateLabelFunc  = studyStateLabelService.stateToLabelFunc(vm.study.state);
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
          .then(function (study) {
            $scope.$emit('study-name-changed', study);
            postUpdate(gettextCatalog.getString('Name changed successfully.'),
                       gettextCatalog.getString('Change successful'))(study);
          })
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

export default ngModule => ngModule.component('studySummary', component)
