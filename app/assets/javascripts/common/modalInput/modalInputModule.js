/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';
import _       from 'lodash';

const MODULE_NAME = 'biobank.modalinput';

/**
 * The functions this service provides.
 */
const modalTypes = [
  'boolean',
  'dateTime',
  'email',
  'number',
  'naturalNumber',
  'password',
  'positiveFloat',
  'select',
  'selectMultiple',
  'text',
  'textArea',
  'url'
];

/**
 * Creates a module with one service and multiple directives to allow the user to modify an entity value
 * while in a modal.
 */
const module = angular.module(MODULE_NAME, []);

module.service('modalInput', modalInputService);
init();

/*
 * Creates the required directives.
 */
function init() {
  _.each(modalTypes, function (type) {
    var name = 'modalInput' + _.upperFirst(type);
    var directive = modalInputDirectiveGenerator(type);
    module.directive(name, directive);
  });
}

function modalInputDirectiveGenerator(type) {
  return function () {
    var directive = {
      restrict: 'E',
      templateUrl : '/assets/javascripts/common/modalInput/' + type + '.html'
    };
    return directive;
  };
}

modalInputService.$inject = ['$uibModal'];

/**
 * Presents a modal to the user where a value for an entity can be updated.
 *
 * See "modalTypes" above for the valid service function names.
 *
 * Each modal function takes the following parameters:
 *
 * @param {string} title The title to display in the modal popup.
 *
 * @param {string} label The label for the field being modified.
 *
 * @param {object} defaultValue the current value for the field being modified.
 *
 * @param {object} options.required When true, the value is required and an error is displayed if the value
 * is set to blank by the user.
 *
 * @param {object} [options.minLength] Used for "text" and "textArea" and specifies the minimum length that
 * is accepted.
 *
 * @param {object} [options.selectOptions] Used for "select" and "selectMultiple", these are the options to
 * be displayed.
 *
 * @return {uibModalInstance} The instance of the modal that was opened. This is a ui-bootstrap class.
 */
function modalInputService($uibModal) {
  var service = {};

  // create a service function for each modal type
  _.each(modalTypes, function (modalType) {
    service[modalType] = function (title, label, defaultValue, options) {
      return modalInput(modalType, title, label, defaultValue, options);
    };
  });

  return service;

  //-------

  /*
   * Displays a modal asking user to enter a value. The type of value depends on the "type" parameter.
   */
  function modalInput(type, title, label, defaultValue, options) {
    var modal;

    modal = $uibModal.open({
      templateUrl: '/assets/javascripts/common/modalInput/modalInput.html',
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: true,
      modalFade: true
    });

    return modal;

    //--

    function ModalController() {
      var vm = this;

      vm.defaultValue = defaultValue;
      vm.options      = options;
      vm.type         = type;
      vm.title        = title;
      vm.label        = label;
      vm.okPressed    = okPressed;
      vm.closePressed = closePressed;
      vm.dateTimeOnEdit = dateTimeOnEdit;

      options = options || {};
      vm.value = vm.defaultValue;

      if (type === 'password') {
        vm.value = {};
      } else if (type === 'selectMultiple') {
        vm.value = getSelectMultipleValues();
        vm.multipleSelectSomeSelected = multipleSelectSomeSelected;
      } else if (type === 'dateTime') {
        vm.value = new Date(vm.defaultValue);
      }

      //--

      function okPressed() {
        modal.close(vm.value);
      }

      function closePressed() {
        modal.dismiss('cancel');
      }

      function getSelectMultipleValues() {
        if (!options.selectOptions) {
          throw new Error('select options not provided');
        }

        if (!_.isArray(vm.defaultValue)) {
          throw new Error('defaultValue is not an array');
        }

        return _.map(options.selectOptions, function (opt) {
          return { name: opt, checked: _.includes(vm.defaultValue, opt)};
        });
      }

      function multipleSelectSomeSelected() {
        return (_.find(vm.value, { checked: true }) !== undefined);
      }

      function dateTimeOnEdit(datetime) {
        vm.value = datetime;
      }
    }
  }

}

export default MODULE_NAME;
