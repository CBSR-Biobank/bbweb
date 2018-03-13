/**
 * This AngularJS Service allows the user to select a {@link domain.access.RoleName RoleName} from a
 * modal that contains a `uib-typeahead`.
 *
 * @memberOf admin.users.services
 */
class MatchingRoleNamesService {

  constructor($q, $log, asyncInputModal, RoleName) {
    'ngInject'
    Object.assign(this, { $q, $log, asyncInputModal, RoleName })
  }

  /**
   * Opens a modal dialog that prompts the user to type the name, or partial name, for a role.
   *
   * <p>Matches for the text entered by the user are then retrieved asynchronously from the server, and the
   * matches are displayed in a Bootstrap Typeahead input (`uib-typeahead`).
   *
   * <p>If the user presses the **OK** button, the object returned by the modal is the object corresponding
   * to the label selected by the user.
   *
   * @param {string} title - the title to display for the modal diaglog box.
   *
   * @param {string} prompt - the prompt to display next to the input field.
   *
   * @param {string} placeholder - a message to display in the input field when no value is present.
   *
   * @param {string} noMatchesMessage - the message to display if the input does not match any role names.
   *
   * @param {Array<domain.access.RoleName>} omit - an array of role names to omit.
   *
   * @return {Promise<domain.access.RoleName>} If the user presses the **OK** button, the role name selected
   * by the user. Otherwise, a rejected promise is returned.
   */
  open(title, prompt, placeholder, noMatchesMessage, namesToOmit) {
    const getMatchingRoleNames = (viewValue) =>
          this.RoleName.list({ filter: 'name:like:' + viewValue}, namesToOmit)
          .then(nameObjs => nameObjs.map((nameObj) => ({ label: nameObj.name, obj: nameObj })))

    return this.asyncInputModal
      .open(title, prompt, placeholder, noMatchesMessage, getMatchingRoleNames)
      .result;
  }

}

export default ngModule => ngModule.service('matchingRoleNames', MatchingRoleNamesService)
