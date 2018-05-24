/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
import DomainModule from '../../../domain'
import _           from 'lodash';
import angular     from 'angular';
import uiBootstrap from 'angular-ui-bootstrap'

/**
 * AngularJS Module that created modals to enter values of different types.
 * @namespace common.modules.modalInput
 */

/**
 * The types of modals this service provides.
 *
 * @memberOf common.modules.modalInput
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
 * Creates a module with one Service and multiple Components to allow the user to modify an entity value while
 * in a modal.
 *
 * @memberOf common.modules.modalInput
 */
const ngModalInputModule = angular.module('biobank.modalinput', [ uiBootstrap, DomainModule ]);

/**
 * Presents a modal to the user where they can enter a value or modify a value.
 *
 * @memberOf common.modules.modalInput
 *
 * @borrows modalInput as common.modules.modalInput.ModalInputService#boolean
 * @borrows modalInput as common.modules.modalInput.ModalInputService#dateTime
 * @borrows modalInput as common.modules.modalInput.ModalInputService#email
 * @borrows modalInput as common.modules.modalInput.ModalInputService#number
 * @borrows modalInput as common.modules.modalInput.ModalInputService#naturalNumber
 * @borrows modalInput as common.modules.modalInput.ModalInputService#password
 * @borrows modalInput as common.modules.modalInput.ModalInputService#positiveFloat
 * @borrows modalInput as common.modules.modalInput.ModalInputService#select
 * @borrows modalInput as common.modules.modalInput.ModalInputService#selectMultiple
 * @borrows modalInput as common.modules.modalInput.ModalInputService#text
 * @borrows modalInput as common.modules.modalInput.ModalInputService#textArea
 * @borrows modalInput as common.modules.modalInput.ModalInputService#url
 */
class ModalInputService {

  constructor($uibModal) {
    'ngInject';
    Object.assign(this, { $uibModal });
  }

  boolean(title, label, defaultValue, options) {
    return this.modalInput('boolean', title, label, defaultValue, options);
  }

  dateTime(title, label, defaultValue, options) {
    return this.modalInput('dateTime', title, label, defaultValue, options);
  }

  email(title, label, defaultValue, options) {
    return this.modalInput('email', title, label, defaultValue, options);
  }

  number(title, label, defaultValue, options) {
    return this.modalInput('number', title, label, defaultValue, options);
  }

  naturalNumber(title, label, defaultValue, options) {
    return this.modalInput('naturalNumber', title, label, defaultValue, options);
  }

  password(title, label, defaultValue, options) {
    return this.modalInput('password', title, label, defaultValue, options);
  }

  positiveFloat(title, label, defaultValue, options) {
    return this.modalInput('positiveFloat', title, label, defaultValue, options);
  }

  select(title, label, defaultValue, options) {
    return this.modalInput('select', title, label, defaultValue, options);
  }

  selectMultiple(title, label, defaultValue, options) {
    return this.modalInput('selectMultiple', title, label, defaultValue, options);
  }

  text(title, label, defaultValue, options) {
    return this.modalInput('text', title, label, defaultValue, options);
  }

  textArea(title, label, defaultValue, options) {
    return this.modalInput('textArea', title, label, defaultValue, options);
  }

  url(title, label, defaultValue, options) {
    return this.modalInput('url', title, label, defaultValue, options);
  }

  /**
   * @private
   */
  modalInput(type, title, label, defaultValue, options = { required: false }) {
    let modal = null;

    class ModalController {

      constructor() {
        Object.assign(this,
                      { type, title, label, defaultValue, options },
                      { value: defaultValue });
        if (type === 'password') {
          this.value = {};
        } else if (type === 'selectMultiple') {
          this.value = this.getSelectMultipleValues();
        } else if (type === 'dateTime') {
          this.value = new Date(this.defaultValue);
        }
      }

      okPressed() {
        modal.close(this.value);
      }

      closePressed() {
        modal.dismiss('cancel');
      }

      getSelectMultipleValues() {
        if (!options.selectOptions) {
          throw new Error('select options not provided');
        }

        if (!_.isArray(this.defaultValue)) {
          throw new Error('defaultValue is not an array');
        }

        return options.selectOptions.map((opt) => ({
          name: opt,
          checked: _.includes(this.defaultValue, opt)
        }));
      }

      multipleSelectSomeSelected() {
        return (_.find(this.value, { checked: true }) !== undefined);
      }

      dateTimeOnEdit(datetime) {
        this.value = datetime;
      }
    }

    modal = this.$uibModal.open({
      template: require('./modalInput.html'),
      controller: ModalController,
      controllerAs: 'vm',
      backdrop: true,
      keyboard: true,
      modalFade: true
    });

    return modal;
  }

}

/**
 * Opens a modal and allows the user to add or modify a value.
 *
 * @callback modalInput
 * @memberOf common.modules.modalInput
 *
 * @param {string} title The title to display in the modal popup.
 *
 * @param {string} label The label for the field being modified.
 *
 * @param {object} defaultValue the current value for the field being modified.
 *
 * @param {boolean} options.required=false When true, the value is required and an error is displayed if the
 * value is set to blank by the user.
 *
 * @param {int} [options.minLength] Used for `text` and `textArea` modals, and specifies the minimum length
 * that is accepted.
 *
 * @param {Array<string>} [options.selectOptions] Used for `select` and `selectMultiple` modals, these are the
 * options to be displayed.
 */

/*
 * Creates the required directives.
 */
function init() {
  ngModalInputModule.service('modalInput', ModalInputService);
  modalTypes.forEach((type) => {
    const name = 'modalInput' + _.upperFirst(type),
          component = modalInputComponentGenerator(type);
    ngModalInputModule.component(name, component);
  });

  function modalInputComponentGenerator(type) {
    const component = {
      template: require('./' + type + '.html'),
      controllerAs: 'vm',
      bindings: {
        label:   '@',
        value:   '<',
        options: '<'
      }
    }
    return component;
  }
}

init();

export default ngModalInputModule.name;
