/**
 * This AngularJS Service allows the user to select a {@link domain.users.UserName UserName} from a
 * modal that contains a `uib-typeahead`.
 *
 * @memberOf ng.admin.users.services
 */
class MatchingUserNamesService {

  constructor(asyncInputModal, UserName) {
    'ngInject'
    Object.assign(this, { asyncInputModal, UserName })
  }

  /**
   * Opens a modal dialog that prompts the user to type the name, or partial name, for a user.
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
   * @param {string} noMatchesMessage - the message to display if the input does not match any user names.
   *
   * @param {Array<domain.users.UserName>} omit - an array of user names to omit.
   *
   * @return {Promise<domain.users.UserName>} If the user presses the **OK** button, the user name selected
   * by the user. Otherwise, a rejected promise is returned.
   */
  open(title, prompt, placeholder, noMatchesMessage, namesToOmit) {
    const getMatchingUserNames = viewValue => {
      let filter = 'name:like:' + viewValue
      if (namesToOmit.length > 0) {
        filter += ';name:out:' + namesToOmit.join(',')
      }
      return this.UserName.list({ filter })
        .then(userNames => userNames.map(user => ({ label: user.name, obj: user })))
    }

    return this.asyncInputModal
      .open(title, prompt, placeholder, noMatchesMessage, getMatchingUserNames)
      .result;
  }

}

export default ngModule => ngModule.service('matchingUserNames', MatchingUserNamesService)
