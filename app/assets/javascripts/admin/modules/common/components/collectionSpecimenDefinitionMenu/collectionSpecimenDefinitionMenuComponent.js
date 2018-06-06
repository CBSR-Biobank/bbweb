/**
 * AngularJS Component used {@link domain.studies.Study Study} administration.
 *
 * @namespace admin.studies.components.collectionSpecimenDefinitionMenu
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
class CollectionSpecimenDefinitionMenuController {

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
 * An AngularJS component that displays a context menu for {@link domain.studies.CollectionSpecimenDefinition
 * CollectionSpecimenDefinitions}.
 *
 * @memberOf admin.studies.components.collectionSpecimenDefinitionMenu
 */
const collectionSpecimenDefinitionMenuComponent = {
  template: require('./collectionSpecimenDefinitionMenu.html'),
  controller: CollectionSpecimenDefinitionMenuController,
  controllerAs: 'vm',
  bindings: {
    specimenDefinition: '<',
    allowChanges: '<',
    onView: '&',
    onUpdate: '&',
    onRemove: '&'
  }
};

export default ngModule => ngModule.component('collectionSpecimenDefinitionMenu',
                                             collectionSpecimenDefinitionMenuComponent);
