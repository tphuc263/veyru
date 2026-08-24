import {Link} from "react-router-dom";
import { DEFAULT_AVATAR } from "../../utils/constants";
import "../../assets/styles/components/userResultItem.css"

export const UserResultItem = ({user}) => (
    <Link to={`/profile/${user.id}`} className="user-result-item">
        <img src={user.imageUrl || DEFAULT_AVATAR} alt={`${user.username}'s avatar`} className="avatar" onError={e => { e.currentTarget.src = DEFAULT_AVATAR; }}/>
        <div className="user-info">
            <span className="username">{user.username}</span>
        </div>
    </Link>
);