class CollectedSpecimenDefinitionMenuController {

  constructor() {
    'ngInject';
    Object.assign(this, {});
  }

  $onInit() {
  }

  view() {
    if (this.onView()) {
      this.onView()(this.specimenDefinition)
    }
  }

  update() {
    if (this.onUpdate()) {
      this.onUpdate()(this.specimenDefinition)
    }
  }

  remove() {
    if (this.onRemove()) {
      this.onRemove()(this.specimenDefinition)
    }
  }
}

/**
 * An AngularJS component that
 *
 * @memberOf
 */
const collectedSpecimenDefinitionMenuComponent = {
  template: require('./collectedSpecimenDefinitionMenu.html'),
  controller: CollectedSpecimenDefinitionMenuController,
  controllerAs: 'vm',
  bindings: {
    specimenDefinition: '<',
    allowChanges: '<',
    onView: '&',
    onUpdate: '&',
    onRemove: '&'
  }
};

export default ngModule => ngModule.component('collectedSpecimenDefinitionMenu',
                                             collectedSpecimenDefinitionMenuComponent);
