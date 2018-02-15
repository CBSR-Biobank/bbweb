/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/**
 * Used to store common data for breadcrumbs.
 *
 * @memberOf ng.common.services
 */
class BreadcrumbService {

  constructor(gettextCatalog) {
    'ngInject'
    Object.assign(this, { gettextCatalog })

    const addLabelFunc  = () =>  gettextCatalog.getString('Add'),
          roleLabelFunc = () => gettextCatalog.getString('Roles')

    this.breadcrumbStateToDisplayFunc = new Map([
      [ 'home',                                 () => gettextCatalog.getString('Home') ],
      [ 'home.about',                           () => gettextCatalog.getString('About') ],
      [ 'home.contact',                         () => gettextCatalog.getString('Contact') ],
      [ 'home.admin',                           () => gettextCatalog.getString('Administration') ],
      [ 'home.admin.studies',                   () => gettextCatalog.getString('Studies') ],
      [ 'home.admin.centres',                   () => gettextCatalog.getString('Centres') ],
      [ 'home.admin.access',                    () => gettextCatalog.getString('Users') ],
      [ 'home.admin.access.users',              () => gettextCatalog.getString('Manage users') ],
      [ 'home.admin.access.users.user.roles',   roleLabelFunc ],
      [ 'home.admin.access.roles',              roleLabelFunc ],
      [ 'home.admin.access.memberships',        () => gettextCatalog.getString('Memberships') ],
      [ 'home.collection',                      () => gettextCatalog.getString('Collection') ],
      [ 'home.shipping',                        () => gettextCatalog.getString('Shipping') ],
      [ 'home.shipping.add',                    () => gettextCatalog.getString('Add shipment') ],
      [ 'home.collection.study.participantAdd', () => gettextCatalog.getString('Add participant') ],
      [ 'home.admin.centres.add',               addLabelFunc ],
      [ 'home.admin.studies.add',               addLabelFunc ],
      [ 'home.admin.access.memberships.add',    addLabelFunc ]
    ]);
  }

  /**
   * Returns the common breadcrumb assigned to this service for the given UI Router state and a function that
   * displays the string associated with the breadcrumb.
   *
   * @param {string} stateName - the state name to return the breadcrumb for.
   *
   * @return {ng.common.services.BreadcrumbService.breadcrumb} the breadcrumb object.
   */
  forState(stateName) {
    if (!this.breadcrumbStateToDisplayFunc.has(stateName)) {
      throw new Error('display name function is undefined for state: ' + stateName);
    }
    const displayNameFunc = this.breadcrumbStateToDisplayFunc.get(stateName)
    return { route: stateName, displayNameFn: displayNameFunc };
  }

  /**
   * Returns a custom breadcrumb object for the given UI Router state. A custom breadcrumb has custom display
   * function.
   *
   * @param {string} stateName - the state name to return the breadcrumb for.
   *
   * @param {function} displayNameFunc - A function that will display the text on the page. This function must
   * be called when the user changes the language.
   *
   * @return {ng.common.services.BreadcrumbService.breadcrumb} the breadcrumb object.
   */
  forStateWithFunc(stateName, displayNameFunc) {
    return { route: stateName, displayNameFn: displayNameFunc };
  }

}

/**
 * This object represents a breadcrumb.
 *
 * <p> Breadcrumbs are used at the top of an HTML page to give the user feedback on where the current page is
 * located in the application.
 *
 * <p>The series of breadcrumbs represent the pages the user has navigated through to get to the current page.
 *
 * <p> Breadcrumbs follow the state hierarchy used by the application.
 *
 * <p> The reason a breadcrumb uses a function is so that the it can be called to display the correct string
 * when the user changes the language.
 *
 * @typedef ng.common.services.BreadcrumbService.breadcrumb
 *
 * @param {string} stateName - The name of the UI Router state in the breadcrumb list.
 *
 * @param {function} displayFun - the function that should be called to display the text for the breadcrumb.
 */

export default ngModule => ngModule.service('breadcrumbService', BreadcrumbService)
